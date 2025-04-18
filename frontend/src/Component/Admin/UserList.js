import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Sidebar from "../Admin/Sidebar";
import LoggedInNavbar from "../LoggedInNavbar";
import Swal from "sweetalert2";

export default function AdminUserList() {
    const [userList, setUserList] = useState([]);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUser = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                navigate("/");
                return;
            }
            try {
                const response = await axios.get("http://localhost:8081/admin/user/list", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setUserList(response.data);
                console.log("User list fetched successfully")
            } catch (err) {
                setError("Please log in again.");
                setTimeout(() => navigate("/"), 2000);
                console.log("Error: ", err);
            }
        };
        fetchUser();
    }, [navigate]);

    const handleDelete = async (userId) => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate('/');
            return;
        }
        if (!window.confirm(`Are you sure you want to delete the user: ${userId}?`)) return;
        try {
            const response = await axios.delete(`http://localhost:8081/admin/user/delete`, {
                headers: { Authorization: `Bearer ${token}` },
                params: { userId }
            });

            await Swal.fire({
                icon: 'success',
                text: response.data.message,
                showConfirmButton: false,
                timer: 1500,
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            setUserList(userList.filter(userObj => userObj.userId !== userId));
        } catch (error) {
            alert(error.response?.data?.error || "Failed to delete user");
        }
    };

    return (
        <>
            <LoggedInNavbar path="/admin/user/list" />

            <div className="container-fluid mt-8">
                <div className="row height">
                    <Sidebar />
                    <main className="col-md-9 col-xl-10 ms-sm-auto">
                        <div className="shadow-lg rounded p-3 my-3">

                            {error && <div className="alert alert-danger my-2">{error}</div>}

                            <div className="table-responsive">
                                <table className="table table-bordered table-striped mx-auto">
                                    <tbody>
                                        {userList.length > 0 ? (
                                            userList.map((data, index) => (
                                                <React.Fragment key={index}>
                                                    <tr>
                                                        <td className="fw-bold bg-light border p-2">Name</td>
                                                        <td className="p-2">{data.name || "No name available"}</td>
                                                    </tr>
                                                    <tr>
                                                        <td className="fw-bold bg-light border p-2">Email</td>
                                                        <td className="p-2">{data.email || "No email available"}</td>
                                                    </tr>
                                                    <tr>
                                                        <td className="fw-bold bg-light border p-2">Action</td>
                                                        <td className="p-2">
                                                            <button className="btn btn-123458 btn-sm" onClick={() => handleDelete(data.userId)}>Delete</button>
                                                        </td>
                                                    </tr>
                                                </React.Fragment>
                                            ))
                                        ) : (
                                            <tr>
                                                <td className="text-center">No user found</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </main>
                </div >
            </div >
        </>
    );
};
