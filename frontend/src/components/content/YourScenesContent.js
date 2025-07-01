import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaRegLightbulb } from 'react-icons/fa';
import { FiPower } from 'react-icons/fi';
import axios from 'axios';
import ModalLayout from '../layout/ModalLayout';

export default function YourScenes() {
    const [scenes, setScenes] = useState([]);
    const [devices, setDevices] = useState([]);
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const token = localStorage.getItem('token');

    const fetchScene = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/rule`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                setScenes(data);
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to fetch schedule';
            console.error(errorMessage);
        }
    }, [token, navigate]);

    const fetchDevice = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/thing`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200 && Array.isArray(data.things)) {
                const devicesList = data.things.map(thing => {
                    const mainItem = thing.items?.find(item =>
                        item.type === 'Switch' || item.type === 'Color' || item.name?.toLowerCase().includes('wiz_bulb_color')) || thing.items?.[0];
                    return {
                        id: mainItem?.name,
                        roomName: thing.roomName,
                        thingName: thing.label,
                        status: mainItem?.type === 'Switch' ? mainItem?.state === 'ON' : mainItem?.state !== '0,0,0'
                    };
                });
                setDevices(devicesList);
            }
        } catch (err) {
            setDevices([]);
            const errorMessage = err.response?.data?.error || 'Failed to fetch device';
            console.error(errorMessage);
        }
    }, [token, navigate]);

    useEffect(() => {
        fetchScene();
        fetchDevice();
        // const intervalId = setInterval(() => {
        //     fetchDevice();
        // }, 3000);
        // return () => clearInterval(intervalId);
    }, [fetchDevice, fetchScene]);

    return (
        <>
            <div className='container px-5 py-4'>

                <div className='d-flex justify-content-between align-items-center mb-3'>
                    <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }}>Your Schedule</div>
                    <Link className='btn btn-dark' to={'/schedule/create_schedule'}>Create Schedule</Link>
                </div>

                {/* Content */}
                <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>
                    {scenes.length === 0 ? (
                        <div className='alert d-flex justify-content-center align-items-center h-100'>No schedule yet to create</div>
                    ) : (
                        scenes.reduce((rows, scenesObj, index) => {
                            if (index % 2 === 0) rows.push([]);
                            rows[rows.length - 1].push(scenesObj);
                            return rows;
                        }, []).map((row, rowIndex) => (
                            <div className="row mx-0 mb-3" key={rowIndex}>
                                {row.map((scenesObj, colIndex) => (
                                    <div className="col-6" key={colIndex}>

                                        <div className="d-flex flex-column border rounded p-3"
                                            style={{ backgroundColor: '#ffffff', width: '100%', height: '168px', cursor: 'pointer' }}
                                            onClick={() => {
                                                console.log('Navigating to scene:', scenesObj);
                                                navigate(`/schedule/update_schedule/${scenesObj.ruleId}`, { state: { scene: scenesObj } });
                                            }}>

                                            <div className="d-flex justify-content-between align-items-center mb-2">

                                                <div>
                                                    <span className='fw-bold'>{scenesObj.ruleName}</span> - {scenesObj.roomName}
                                                </div>

                                                <div className="form-check form-switch">
                                                    <input
                                                        className="form-check-input"
                                                        type="checkbox"
                                                        role="switch"
                                                        checked={scenesObj.enabled}
                                                        onClick={(e) => e.stopPropagation()}
                                                        onChange={async (e) => {
                                                            e.stopPropagation();
                                                            const newEnabled = e.target.checked;
                                                            try {
                                                                await axios.patch(`${process.env.REACT_APP_API_URL}/user/rule/toggle`,
                                                                    { ruleId: scenesObj.ruleId, enable: newEnabled },
                                                                    { headers: { Authorization: `Bearer ${token}` } }
                                                                );
                                                                setScenes(prevScenes =>
                                                                    prevScenes.map(scene =>
                                                                        scene.ruleId === scenesObj.ruleId ? { ...scene, enabled: newEnabled } : scene
                                                                    )
                                                                );
                                                            } catch (err) {
                                                                const errorMessage = err.response?.data?.error || 'Failed to update rule';
                                                                setModal({
                                                                    show: true,
                                                                    title: 'Failed',
                                                                    message: <span className='text-danger'>{errorMessage}</span>,
                                                                    isError: true,
                                                                    onConfirm: () => setModal({ ...modal, show: false }),
                                                                });
                                                            }
                                                        }}
                                                    />
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
                                                            {(() => {
                                                                const device = devices.find(dev =>
                                                                    dev.roomName === scenesObj.roomName
                                                                );
                                                                return device?.status ? 'ON' : 'OFF';
                                                            })()}
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

            </div>

            {/* Alert Modal */}
            {modal.show && (
                <ModalLayout title={modal.title} msg={modal.message} modal={modal.onConfirm} hideClose={!modal.isError}>
                    <button onClick={modal.onConfirm} className={`btn btn-dark px-3`}>
                        {modal.isError ? 'Try Again' : 'OK'}
                    </button>
                </ModalLayout>
            )}
        </>
    );
};