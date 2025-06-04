import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaRegLightbulb } from 'react-icons/fa';
import { FiPower } from 'react-icons/fi';
import axios from 'axios';

export default function YourScenes() {
    const [scenes, setScenes] = useState([]);
    const [devices, setDevices] = useState([]);
    const navigate = useNavigate();

    const token = localStorage.getItem('token');

    const fetchScenes = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.get('http://localhost:8081/user/scenes', {
                headers: { Authorization: `Bearer ${token}` }
            });
            if (status === 200) {
                setScenes(data);
            }
        } catch (err) {
            const error = err.response?.data?.error || 'Scenes fetch failed';
            console.error('Error:', error);
        }
    }, [token, navigate]);

    const fetchDevices = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const response = await axios.get('http://localhost:8081/user/device', {
                headers: { Authorization: `Bearer ${token}` }
            });

            const { data, status } = response;

            if (status === 200 && Array.isArray(data.things)) {
                const devicesList = data.things.map(thing => {
                    const mainItem = thing.items?.find(item =>
                        item.type === 'Switch' ||
                        item.type === 'Color' ||
                        item.name?.toLowerCase().includes('wiz_bulb_color')
                    ) || thing.items?.[0];

                    const state = mainItem?.state;
                    const isOn = state === 'ON' || state === '1' || state === '255,255,255';

                    console.log(state, isOn);

                    return {
                        id: mainItem?.name,
                        roomName: thing.roomName,
                        deviceName: thing.label,
                        status: isOn
                    };
                });
                setDevices(devicesList);
            } else {
                console.warn('Unexpected response format:', data);
                setDevices([]);
            }
        } catch (err) {
            const error = err.response?.data?.error || 'Error fetching devices';
            console.error('Error fetching devices:', error);
            setDevices([]);
        }
    }, [token, navigate]);

    useEffect(() => {
        fetchScenes();
        fetchDevices();

        // const intervalId = setInterval(() => {
        //     fetchDevices();
        // }, 3000);

        // return () => clearInterval(intervalId);
    }, [fetchDevices, fetchScenes]);

    return (
        <>
            <div className='container px-5 py-4'>
                <div className='d-flex justify-content-between align-items-center mb-3'>
                    <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }}>Your Scenes</div>
                    <Link className='btn btn-dark' to={'/schedule/create_scenes'}>Create Scene</Link>
                </div>

                <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>
                    {scenes.length === 0 ? (
                        <div className='alert d-flex justify-content-center align-items-center h-100'>No scenes yet to create</div>
                    ) : (
                        scenes.reduce((rows, scenesObj, index) => {
                            if (index % 2 === 0) rows.push([]);
                            rows[rows.length - 1].push(scenesObj);
                            return rows;
                        }, []).map((row, rowIndex) => (
                            <div className="row mx-0 mb-3" key={rowIndex}>
                                {row.map((scenesObj, colIndex) => (
                                    <div className="col-6" key={colIndex}>
                                        <div className="d-flex flex-column border rounded p-3" style={{ backgroundColor: '#ffffff', width: '100%', height: '168px', cursor: 'pointer' }} >
                                            <div className="d-flex justify-content-between align-items-center mb-2">
                                                <div>
                                                    <span className='fw-bold'>{scenesObj.scenesName || 'My Scene'}</span> - {scenesObj.roomName || 'Room'}
                                                </div>
                                                <div className="form-check form-switch">
                                                    <input className="form-check-input" type="checkbox" role="switch" />
                                                </div>
                                            </div>

                                            <div className="text-muted mb-2">
                                                {scenesObj.fromTime} to {scenesObj.toTime}
                                            </div>

                                            <div className="d-flex flex-wrap justify-content-between align-items-center mb-2">
                                                <div className='d-flex flex-column justify-content-center align-items-center bg-eaeaea rounded p-2'>
                                                    <FaRegLightbulb className='text-muted mb-1' style={{ width: '30px', height: '30px' }} />
                                                    <div className='d-flex justify-content-center align-items-center'>
                                                        <FiPower className='mx-1' />
                                                        <div className='mx-1'>
                                                            {
                                                                (() => {
                                                                    const device = devices.find(dev =>
                                                                        dev.roomName === scenesObj.roomName
                                                                    );
                                                                    return device?.status ? 'ON' : 'OFF';
                                                                })()
                                                            }
                                                        </div>
                                                    </div>

                                                </div>
                                                <div className='d-flex gap-1'>
                                                    {scenesObj.days.map((dayObj, index) => (
                                                        <React.Fragment key={index}>
                                                            <span className="d-flex justify-content-center align-items-center rounded-circle bg-dark text-white" style={{ height: '35px', width: '35px', fontSize: '12px' }}>
                                                                {dayObj.charAt(0) + dayObj.slice(1).toLowerCase().slice(0, 2)}
                                                            </span>
                                                        </React.Fragment>
                                                    ))}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ))
                    )}
                </div>
                {/* 
                <div className="text-end my-5">
                    <button className="btn btn-dark" onClick={() => navigate('/schedule/create_scenes')}>Create</button>
                </div> */}
            </div>
        </>
    );
};